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
 * Status codes returned by the JACK server.
 */

public enum JackStatusCode
{
  /**
   * Overall operation failed.
   */

  JACK_FAILURE,

  /**
   * The operation contained an invalid or unsupported option.
   */

  JACK_INVALID_OPTION,

  /**
   * The desired client name was not unique.
   */

  JACK_NAME_NOT_UNIQUE,

  /**
   * The JACK server was started as a result of this operation.
   * Otherwise, it was running already.  In either case the caller
   * is now connected to jackd, so there is no race condition.
   * When the server shuts down, the client will find out.
   */

  JACK_SERVER_STARTED,

  /**
   * Unable to connect to the JACK server.
   */

  JACK_SERVER_FAILED,

  /**
   * Communication error with the JACK server.
   */

  JACK_SERVER_ERROR,

  /**
   * Requested client does not exist.
   */

  JACK_NO_SUCH_CLIENT,

  /**
   * Unable to load internal client.
   */

  JACK_LOAD_FAILURE,

  /**
   * Unable to initialize client.
   */

  JACK_INIT_FAILURE,

  /**
   * Unable to access shared memory.
   */

  JACK_SHM_FAILURE,

  /**
   * Client's protocol version does not match.
   */

  JACK_VERSION_ERROR,

  /**
   * Backend error.
   */

  JACK_BACKEND_ERROR,

  /**
   * Client zombified failure.
   */

  JACK_CLIENT_ZOMBIE
}
