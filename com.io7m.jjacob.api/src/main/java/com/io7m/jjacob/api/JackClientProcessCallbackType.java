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
 * A callback function that is called whenever the JACK server requires
 * the client to populate a buffer with samples.
 */

public interface JackClientProcessCallbackType
{
  /**
   * Called when processing is required. The given {@code context} value
   * is valid only for the lifetime of the {@code onProcess} call and <i>MUST NOT</i>
   * be stored or otherwise used outside of the callback method.
   *
   * @param context The callback context
   *
   * @throws Exception On errors
   */

  void onProcess(
    JackClientProcessCallbackContextType context)
    throws Exception;
}
