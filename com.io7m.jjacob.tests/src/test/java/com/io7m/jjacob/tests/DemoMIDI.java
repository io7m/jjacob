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
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.jjacob.jnr.LibJackOptions.JackNoStartServer;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsInput;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsOutput;

public final class DemoMIDI
{
  private static final Logger LOG = LoggerFactory.getLogger(DemoMIDI.class);

  private DemoMIDI()
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
      final Pointer port =
        jack.jack_port_register(
          client,
          "in_M",
          LibJackPorts.defaultMidiType(),
          (long) JackPortIsInput.intValue(),
          0L);

      if (port.address() == 0L) {
        LOG.error("could not create port: {}", Integer.valueOf(status[0]));
        throw new RuntimeException();
      }

      {
        LOG.debug("setting client process callback");
        jack.jack_set_process_callback(client, (frames, data) -> {
          final Thread current = Thread.currentThread();

          final Pointer buffer = jack.jack_port_get_buffer(port, frames);
          final int count = jack.jack_midi_get_event_count(buffer);

          final LibJackType.MidiEvent event =
            new LibJackType.MidiEvent(Runtime.getSystemRuntime());
          for (int index = 0; index < count; ++index) {
            final int r = jack.jack_midi_event_get(event, buffer, index);
            if (r != 0) {
              throw new IllegalStateException();
            }
          }

          if (count > 0) {
            LOG.debug("count: {}", Integer.valueOf(count));
          }
          return 0;
        }, null);
      }

      {
        LOG.debug("activating jack client");
        final int r = jack.jack_activate(client);
        if (r != 0) {
          LOG.error("could not activate jack client: {}", r);
          throw new RuntimeException();
        }
      }

      try {
        while (true) {
          Thread.sleep(30_000L);
        }
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
