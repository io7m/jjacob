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

import jnr.ffi.LibraryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default LibJack provider.
 */

public final class LibJack implements LibJackProviderType
{
  private static final Logger LOG = LoggerFactory.getLogger(LibJack.class);

  /**
   * Construct a provider.
   */

  public LibJack()
  {
    // Nothing!
  }

  /**
   * Get a LibJack implementation.
   *
   * @return a LibJack implementation
   *
   * @throws LibJackUnavailableException If no implementation is available
   */

  public static LibJackType get()
    throws LibJackUnavailableException
  {
    return new LibJack().create();
  }

  @Override
  public LibJackType create()
    throws LibJackUnavailableException
  {
    try {
      LOG.debug("creating libjack loader");
      final LibraryLoader<LibJackType> libjack_loader =
        LibraryLoader.create(LibJackType.class);
      libjack_loader.failImmediately();

      LOG.debug("loading libjack library");
      final LibJackType libjack = libjack_loader.load("jack");
      LOG.debug("loaded libjack library: {}", libjack);

      return libjack;
    } catch (final UnsatisfiedLinkError e) {
      throw new LibJackUnavailableException(e);
    }
  }
}
