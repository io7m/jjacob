/*
 * Copyright © 2018 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jjacob.tests;

import com.io7m.jjacob.api.JackBufferType;
import com.io7m.jjacob.api.JackClientConfiguration;
import com.io7m.jjacob.api.JackClientProviderType;
import com.io7m.jjacob.api.JackClientType;
import com.io7m.jjacob.api.JackException;
import com.io7m.jjacob.api.JackPortFlag;
import com.io7m.jjacob.api.JackPortType;
import com.io7m.jjacob.api.JackPortTypeRegistry;
import com.io7m.jjacob.jnr.LibJack;
import com.io7m.jjacob.jnr.LibJackUnavailableException;
import com.io7m.jjacob.vanilla.JackClientProvider;
import com.io7m.jjacob.vanilla.JackPortTypesDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_INPUT;
import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_OUTPUT;

public final class DemoVanilla
{
  private static final Logger LOG = LoggerFactory.getLogger(DemoVanilla.class);

  private DemoVanilla()
  {

  }

  public static void main(final String[] args)
    throws LibJackUnavailableException
  {
    /*
     * Create a new registry of port types including only those types that
     * JACK knows about by default (JACK_DEFAULT_AUDIO_TYPE and JACK_DEFAULT_MIDI_TYPE).
     */

    final JackPortTypeRegistry types = new JackPortTypeRegistry();
    types.providerAdd(new JackPortTypesDefault());

    /*
     * Create a new client provider using the plain libjack C implementation.
     */

    final JackClientProviderType provider =
      JackClientProvider.create(types, LibJack.get());

    /*
     * Create a basic client configuration that just specifies a client name
     * but otherwise uses the default settings.
     */

    final JackClientConfiguration config =
      JackClientConfiguration.builder()
        .setClientName("jjacob-vanilla")
        .build();

    /*
     * Open a new client.
     */

    try (final JackClientType client = provider.openClient(config)) {
      LOG.debug("client:      {}", client);
      LOG.debug("sample rate: {}", Integer.valueOf(client.sampleRate()));
      LOG.debug("buffer size: {}", Integer.valueOf(client.bufferSize()));
      LOG.debug("cpu load:    {}", Float.valueOf(client.cpuLoad()));

      /*
       * Show all available input and output ports.
       */

      final List<String> output_ports =
        client.portsList(
          Optional.empty(),
          Optional.empty(),
          EnumSet.of(JACK_PORT_IS_OUTPUT));

      output_ports.forEach(name -> LOG.debug("output: {}", name));

      final List<String> input_ports =
        client.portsList(
          Optional.empty(),
          Optional.empty(),
          EnumSet.of(JACK_PORT_IS_INPUT));

      input_ports.forEach(name -> LOG.debug("input:  {}", name));

      /*
       * Searching for nonexistent ports returns no ports.
       */

      {
        final List<String> empty_ports =
          client.portsList(
            Optional.of("DOES NOT EXIST!"),
            Optional.empty(),
            EnumSet.noneOf(JackPortFlag.class));
        LOG.debug("empty_ports: {}", empty_ports);
      }

      /*
       * Register two new output ports.
       */

      final JackPortType port_L =
        client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));
      final JackPortType port_R =
        client.portRegister("out_R", EnumSet.of(JACK_PORT_IS_OUTPUT));

      LOG.debug("owned: {}", Boolean.valueOf(port_L.belongsTo(client)));
      LOG.debug("owned: {}", Boolean.valueOf(port_R.belongsTo(client)));

      /*
       * Set a processing callback that will write a sine wave tone to the
       * outputs.
       */

      client.setProcessCallback(context -> {
        final JackBufferType buf0 = context.portBuffer(port_L);
        final JackBufferType buf1 = context.portBuffer(port_R);
        final int frames = context.bufferFrameCount();

        for (int index = 0; index < frames; index++) {
          final double position = (double) index / (double) frames;
          final double x = 0.2 * Math.sin(position * Math.PI * 4.0);
          buf0.putF(index, (float) x);
          buf1.putF(index, (float) x);
        }
      });

      /*
       * Activate the client.
       */

      client.activate();

      LOG.debug(
        "port_L: {} ({}) (typeName: '{}') (flags: {})",
        port_L.name(),
        port_L.shortName(),
        port_L.typeName(),
        port_L.flags());
      LOG.debug(
        "port_R: {} ({}) (typeName: '{}') (flags: {})",
        port_R.name(),
        port_R.shortName(),
        port_R.typeName(),
        port_R.flags());

      /*
       * Try and find the system's output ports and connect the client
       * to them.
       */

      final Optional<JackPortType> system_L_opt =
        client.portByName("system:playback_1");
      final Optional<JackPortType> system_R_opt =
        client.portByName("system:playback_2");

      system_L_opt.ifPresent(out_L -> {
        try {
          client.portsConnect(port_L.name(), out_L.name());
        } catch (final JackException e) {
          LOG.error("could not connect output port: ", e);
        }
      });

      system_R_opt.ifPresent(out_R -> {
        try {
          client.portsConnect(port_R.name(), out_R.name());
        } catch (final JackException e) {
          LOG.error("could not connect output port: ", e);
        }
      });

      /*
       * Sleep forever whilst JACK calls the client repeatedly to produce
       * audio.
       */

      try {
        while (true) {
          Thread.sleep(1000L);
        }
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      client.deactivate();
    } catch (final JackException e) {
      LOG.error("jack error: ", e);
    }
  }
}
