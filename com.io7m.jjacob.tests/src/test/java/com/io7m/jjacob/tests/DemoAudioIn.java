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

import java.util.ArrayList;
import java.util.LinkedList;

import static com.io7m.jjacob.jnr.LibJackOptions.JackNoStartServer;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsInput;

public final class DemoAudioIn
{
  private static final Logger LOG = LoggerFactory.getLogger(DemoAudioIn.class);

  private DemoAudioIn()
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
      final LinkedList<Double> sums = new LinkedList<>();

      final Pointer port =
        jack.jack_port_register(
          client,
          "input",
          LibJackPorts.defaultAudioType(),
          (long) JackPortIsInput.intValue(),
          0L);

      if (port.address() == 0L) {
        LOG.error("could not create port: {}", Integer.valueOf(status[0]));
        throw new RuntimeException();
      }

      {
        LOG.debug("setting client process callback");
        jack.jack_set_process_callback(client, (frames, data) -> {
          final Pointer buffer = jack.jack_port_get_buffer(port, frames);

          float sum = 0.0f;
          long offset = 0L;
          for (int index = 0; index < frames; ++index) {
            sum += buffer.getFloat(offset);
            offset += 4L;
          }
          synchronized (sums) {
            sums.add(Double.valueOf((double) sum));
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

      {
        LOG.debug("connecting jack client");
        final int r =
          jack.jack_connect(
            client,
            "eggplant:capture_1",
            "jjacob:input");
        if (r != 0) {
          LOG.error("could not connect jack client: {}", r);
          throw new RuntimeException();
        }
      }

      final ArrayList<Double> sums_copy = new ArrayList<>(1000);
      while (true) {
        try {
          sums_copy.clear();
          synchronized (sums) {
            sums_copy.addAll(sums);
            sums.clear();
          }

          double average = 0.0;
          for (int index = 0; index < sums_copy.size(); ++index) {
            average += sums_copy.get(index).doubleValue();
          }
          average = average / Math.max(1.0, (double) sums_copy.size());
          LOG.debug("average: {}", Double.valueOf(average));
          Thread.sleep(1000L);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
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
