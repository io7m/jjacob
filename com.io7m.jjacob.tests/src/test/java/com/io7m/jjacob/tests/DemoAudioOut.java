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

import com.io7m.jjacob.jnr.LibJack;
import com.io7m.jjacob.jnr.LibJackPorts;
import com.io7m.jjacob.jnr.LibJackType;
import com.io7m.jjacob.jnr.LibJackUnavailableException;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static com.io7m.jjacob.jnr.LibJackOptions.JackNoStartServer;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsOutput;

public final class DemoAudioOut
{
  private static final Logger LOG = LoggerFactory.getLogger(DemoAudioOut.class);

  private DemoAudioOut()
  {

  }

  public static void main(final String[] args)
    throws LibJackUnavailableException
  {
    final LibJackType jack = LibJack.get();

    LOG.debug("opening jack client");

    final int[] status = new int[1];
    final Pointer client =
      jack.jack_client_open(
        "jjacob",
        JackNoStartServer.intValue(),
        status,
        null);

    if (client.address() == 0L) {
      LOG.error("could not create client: {}", Integer.valueOf(status[0]));
      throw new RuntimeException();
    }

    try {
      {
        LOG.debug(
          "sample rate: {}",
          Integer.valueOf(jack.jack_get_sample_rate(client)));
      }

      final Pointer port =
        jack.jack_port_register(
          client,
          "output",
          LibJackPorts.defaultAudioType(),
          (long) JackPortIsOutput.intValue(),
          0L);

      if (port.address() == 0L) {
        LOG.error("could not create port: {}", Integer.valueOf(status[0]));
        throw new RuntimeException();
      }

      {
        LOG.debug("setting client process callback");

        final Pointer raw_data =
          Memory.allocateDirect(Runtime.getSystemRuntime(), 4);

        raw_data.putFloat(0L, 0.0f);

        jack.jack_set_process_callback(client, (frames, data) -> {
          final Pointer buffer = jack.jack_port_get_buffer(port, frames);

          final float rising = data.getFloat(0L) + 0.001f;
          data.putFloat(0L, rising);

          for (int index = 0; index < frames; ++index) {
            final double d_index = (double) index;
            final double d_frames = (double) frames;
            final double d_position = (d_index / d_frames);

            buffer.putFloat(
              (long) index * 4L,
              (float) Math.sin((d_position * Math.PI * 2.0)) * rising);
          }
          return 0;
        }, raw_data);
      }

      {
        LOG.debug("activating jack client");
        final int r = jack.jack_activate(client);
        if (r != 0) {
          LOG.error("could not activate jack client: {}", r);
          throw new RuntimeException();
        }
      }

      {
        LOG.debug("connecting jack client");
        final int r =
          jack.jack_connect(
            client,
            "jjacob:output",
            "system:playback_1");
        if (r != 0) {
          LOG.error("could not connect jack client: {}", r);
          throw new RuntimeException();
        }
      }

      {
        LOG.debug("connecting jack client");
        final int r =
          jack.jack_connect(
            client,
            "jjacob:output",
            "Example Scope (Mono):In");
        if (r != 0) {
          LOG.error("could not connect jack client: {}", r);
          throw new RuntimeException();
        }
      }

      try {
        Thread.sleep(30_000L);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      {
        LOG.debug("deactivating jack client");
        final int r = jack.jack_deactivate(client);
        if (r != 0) {
          LOG.error("could not deactivate jack client: {}", r);
          throw new RuntimeException();
        }
      }
    } finally {
      LOG.debug("closing jack client");
      final int r = jack.jack_client_close(client);
      if (r != 0) {
        LOG.error("could not close jack client: {}", r);
      }
    }
  }
}
