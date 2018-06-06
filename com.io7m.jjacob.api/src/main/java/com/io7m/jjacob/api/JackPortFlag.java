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

package com.io7m.jjacob.api;

/**
 * A flag used when registering a port.
 */

public enum JackPortFlag
{
  /**
   * The port can receive data.
   */

  JACK_PORT_IS_INPUT,

  /**
   * Data can be read from the port.
   */

  JACK_PORT_IS_OUTPUT,

  /**
   * The port corresponds to some kind of physical I/O connector.
   */

  JACK_PORT_IS_PHYSICAL,

  /**
   * XXX: Gibberish
   */

  JACK_PORT_CAN_MONITOR,

  /**
   * For an input port: the data received by the port will not be passed on or
   * made available at any other port.
   *
   * For an output port: the data available at the port does not originate from
   * any other port.
   *
   * Audio synthesizers, I/O hardware interface clients, HDR systems are examples
   * of clients that would set this flag for their portsList.
   */

  JACK_PORT_IS_TERMINAL
}
