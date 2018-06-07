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

import com.io7m.jjacob.api.JackBufferMIDIType;
import com.io7m.jjacob.api.JackClientConfiguration;
import com.io7m.jjacob.api.JackClientProviderType;
import com.io7m.jjacob.api.JackClientType;
import com.io7m.jjacob.api.JackException;
import com.io7m.jjacob.api.JackPortType;
import com.io7m.jjacob.api.JackPortTypeRegistry;
import com.io7m.jjacob.jnr.LibJack;
import com.io7m.jjacob.jnr.LibJackPorts;
import com.io7m.jjacob.jnr.LibJackUnavailableException;
import com.io7m.jjacob.vanilla.JackClientProvider;
import com.io7m.jjacob.vanilla.JackPortTypesDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_INPUT;

public final class DemoVanillaMIDI
{
  private static final Logger LOG = LoggerFactory.getLogger(DemoVanillaMIDI.class);

  private DemoVanillaMIDI()
  {

  }

  public static void main(final String[] args)
    throws LibJackUnavailableException
  {
    final JackPortTypeRegistry types = new JackPortTypeRegistry();
    types.providerAdd(new JackPortTypesDefault());

    final JackClientProviderType provider =
      JackClientProvider.create(types, LibJack.get());

    final JackClientConfiguration config =
      JackClientConfiguration.builder()
        .setClientName("jjacob-vanilla")
        .build();

    try (final JackClientType client = provider.openClient(config)) {
      LOG.debug("client:      {}", client);
      LOG.debug("sample rate: {}", Integer.valueOf(client.sampleRate()));
      LOG.debug("buffer size: {}", Integer.valueOf(client.bufferSize()));
      LOG.debug("cpu load:    {}", Float.valueOf(client.cpuLoad()));

      final JackPortType port_L =
        client.portRegister(
          "in_M",
          LibJackPorts.defaultMidiType(),
          EnumSet.of(JACK_PORT_IS_INPUT),
          0L);

      client.setProcessCallback(context -> {
        final JackBufferMIDIType buf0 = context.portBufferMIDI(port_L);

        final int count = buf0.eventCount();
        final int lost_count = buf0.eventLostCount();
        LOG.debug("lost: {}", Integer.valueOf(lost_count));
      });

      client.activate();

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
