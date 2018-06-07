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
 * A context value passed to <i>process</i> callbacks. The context value
 * <i>is only valid during the call to
 * {@link JackClientProcessCallbackType#onProcess(JackClientProcessCallbackContextType)}</i>
 * and <i>MUST NOT</i> be stored or otherwise used outside of that callback.
 *
 * @see JackClientType#setProcessCallback(JackClientProcessCallbackType)
 * @see JackClientProcessCallbackType
 */

public interface JackClientProcessCallbackContextType
{
  /**
   * @return The number of frames that must be processed by the callback
   *
   * @throws JackException On errors
   */

  int bufferFrameCount()
    throws JackException;

  /**
   * Get a reference to the buffer for the target port. The buffer <i>is only
   * valid during the call to
   * {@link JackClientProcessCallbackType#onProcess(JackClientProcessCallbackContextType)}</i>
   * and <i>MUST NOT</i> be stored or otherwise used outside of that callback.
   *
   * @param port The target port
   *
   * @return A port buffer
   *
   * @throws JackException On errors
   */

  JackBufferType portBuffer(
    JackPortType port)
    throws JackException;

  /**
   * Get a reference to the buffer for the target port. The buffer <i>is only
   * valid during the call to
   * {@link JackClientProcessCallbackType#onProcess(JackClientProcessCallbackContextType)}</i>
   * and <i>MUST NOT</i> be stored or otherwise used outside of that callback.
   *
   * The method will raise an exception if the port is not of a type understood
   * by JACK to contain MIDI event data.
   *
   * @param port The target port
   *
   * @return A port buffer
   *
   * @throws JackException On errors
   */

  JackBufferMIDIType portBufferMIDI(
    JackPortType port)
    throws JackException;
}
