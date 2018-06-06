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

import java.util.Set;

/**
 * A port registered by a client of the JACK server.
 */

public interface JackPortType
{
  /**
   * Retrieve the client connection used to either register or find this port.
   * If the client connection is closed, all methods on this port instance
   * will raise exceptions.
   *
   * @return The client connection used to either register or find this port
   */

  JackClientType connection();

  /**
   * @return The short name of the port
   *
   * @throws JackException On errors
   * @see "jack_port_short_name"
   */

  String shortName()
    throws JackException;

  /**
   * @return The full name of the port
   *
   * @throws JackException On errors
   * @see "jack_port_name"
   */

  String name()
    throws JackException;

  /**
   * @return The type of the port
   *
   * @throws JackException On errors
   * @see "jack_port_type"
   */

  String type()
    throws JackException;

  /**
   * @return The flags of the port
   *
   * @throws JackException On errors
   * @see "jack_port_flags"
   */

  Set<JackPortFlag> flags()
    throws JackException;

  /**
   * @param client The target client
   *
   * @return {@code true} iff {@code client} owns this port
   *
   * @throws JackException On errors
   * @see "jack_port_is_mine"
   */

  boolean belongsTo(
    JackClientType client)
    throws JackException;
}
