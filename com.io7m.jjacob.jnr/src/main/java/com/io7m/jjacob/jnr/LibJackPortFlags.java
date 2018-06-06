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

package com.io7m.jjacob.jnr;

import jnr.ffi.util.EnumMapper;

/**
 * Flags used when registering ports.
 */

public enum LibJackPortFlags implements EnumMapper.IntegerEnum
{
  /**
   * if JackPortIsInput is set, then the port can receive
   * data.
   */

  JackPortIsInput(0x1),

  /**
   * if JackPortIsOutput is set, then data can be read from
   * the port.
   */

  JackPortIsOutput(0x2),

  /**
   * if JackPortIsPhysical is set, then the port corresponds
   * to some kind of physical I/O connector.
   */

  JackPortIsPhysical(0x4),

  /**
   * if JackPortCanMonitor is set, then a call to
   * jack_port_request_monitor() makes sense.
   *
   * Precisely what this means is dependent on the client. A typical
   * result of it being called with TRUE as the second argument is
   * that data that would be available from an output port (with
   * JackPortIsPhysical set) is sent to a physical output connector
   * as well, so that it can be heard/seen/whatever.
   *
   * Clients that do not control physical interfaces
   * should never create ports with this bit set.
   */

  JackPortCanMonitor(0x8),

  /**
   * JackPortIsTerminal means:
   *
   * for an input port: the data received by the port
   * will not be passed on or made
   * available at any other port
   *
   * for an output port: the data available at the port
   * does not originate from any other port
   *
   * Audio synthesizers, I/O hardware interface clients, HDR
   * systems are examples of clients that would set this flag for
   * their ports.
   */

  JackPortIsTerminal(0x10);

  private int value;

  LibJackPortFlags(final int i)
  {
    this.value = i;
  }

  @Override
  public int intValue()
  {
    return this.value;
  }
}
