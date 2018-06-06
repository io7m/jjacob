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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Configuration values for client connections.
 */

@ImmutablesStyleType
@Value.Immutable
public interface JackClientConfigurationType
{
  /**
   * @return The name that will be used for the client
   */

  @Value.Parameter
  Optional<String> clientName();

  /**
   * If a client specifies a name with {@link #clientName()}, the JACK server
   * is free to pick a modified version of the name if there is already a client
   * using the given name. If "exact naming" is specified, then the attempt
   * to connect to the JACK server will raise an exception rather than allow
   * the assignment of a new modified name.
   *
   * @return {@code true} if exact client naming should be used
   *
   * @see #clientName()
   */

  @Value.Parameter
  @Value.Default
  default boolean clientNameUseExact()
  {
    return false;
  }

  /**
   * @return The name of the server to which to connect
   */

  @Value.Parameter
  Optional<String> serverName();

  /**
   * @return {@code true} if a JACK server should be started if one is not
   * already running
   */

  @Value.Parameter
  @Value.Default
  default boolean startServer()
  {
    return false;
  }
}
