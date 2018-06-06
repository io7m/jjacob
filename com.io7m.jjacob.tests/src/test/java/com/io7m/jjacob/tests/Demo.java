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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.jjacob.jnr.LibJackOptions.JackNoStartServer;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsInput;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsOutput;

public final class Demo
{
  private static final Logger LOG = LoggerFactory.getLogger(Demo.class);

  private Demo()
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

      {
        LOG.debug("setting client process callback");
        jack.jack_set_process_callback(client, (frames, data) -> {
          // LOG.debug("process: {}", Integer.valueOf(frames));
          return 0;
        }, null);
      }

      {
        LOG.debug("setting client shutdown callback");
        jack.jack_on_shutdown(client, (data) -> {
          LOG.debug("shutdown");
        }, null);
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
        LOG.debug("activating jack client");
        final int r = jack.jack_activate(client);
        if (r != 0) {
          LOG.error("could not activate jack client: {}", r);
          throw new RuntimeException();
        }
      }

      {
        final Pointer ports =
          jack.jack_get_ports(
          client,
          null,
          null,
          (long) (JackPortIsOutput.intValue()));

        LOG.debug("portsList:   0x{}", ports);
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
