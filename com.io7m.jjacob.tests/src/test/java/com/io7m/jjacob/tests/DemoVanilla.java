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
import com.io7m.jjacob.jnr.LibJack;
import com.io7m.jjacob.jnr.LibJackUnavailableException;
import com.io7m.jjacob.vanilla.JackClientProvider;
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
    final JackClientProviderType provider =
      JackClientProvider.create(LibJack.get());

    final JackClientConfiguration config =
      JackClientConfiguration.builder()
        .setClientName("jjacob-vanilla")
        .build();

    try (final JackClientType client = provider.openClient(config)) {
      LOG.debug("client:      {}", client);
      LOG.debug("sample rate: {}", Integer.valueOf(client.sampleRate()));
      LOG.debug("buffer size: {}", Integer.valueOf(client.bufferSize()));
      LOG.debug("cpu load:    {}", Float.valueOf(client.cpuLoad()));

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

      input_ports.forEach(name ->  LOG.debug("input:  {}", name));

      {
        final List<String> empty_ports =
          client.portsList(
            Optional.of("DOES NOT EXIST!"),
            Optional.empty(),
            EnumSet.noneOf(JackPortFlag.class));
        LOG.debug("empty_ports: {}", empty_ports);
      }

      final JackPortType port_L =
        client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));
      final JackPortType port_R =
        client.portRegister("out_R", EnumSet.of(JACK_PORT_IS_OUTPUT));

      LOG.debug("owned: {}", Boolean.valueOf(port_L.belongsTo(client)));
      LOG.debug("owned: {}", Boolean.valueOf(port_R.belongsTo(client)));

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

      client.activate();

      LOG.debug(
        "port_L: {} ({}) (type: '{}') (flags: {})",
        port_L.name(),
        port_L.shortName(),
        port_L.type(),
        port_L.flags());
      LOG.debug(
        "port_R: {} ({}) (type: '{}') (flags: {})",
        port_R.name(),
        port_R.shortName(),
        port_R.type(),
        port_R.flags());

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
