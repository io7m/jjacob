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

import com.io7m.jjacob.jnr.LibJackType;
import jnr.ffi.Pointer;

public class LibJackUnsupported implements LibJackType
{
  public LibJackUnsupported()
  {

  }

  @Override
  public Pointer jack_client_open(
    final String name,
    final int options,
    final int[] status,
    final String server_name)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_client_close(final Pointer client)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String jack_get_client_name(final Pointer client)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_get_sample_rate(final Pointer client)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_get_buffer_size(final Pointer client)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public float jack_cpu_load(final Pointer client)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_set_process_callback(
    final Pointer client,
    final ProcessCallbackType process,
    final Pointer data)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pointer jack_port_register(
    final Pointer client,
    final String port_name,
    final String port_type,
    final long flags,
    final long buffer_size)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void jack_on_shutdown(
    final Pointer client,
    final ShutdownCallbackType process,
    final Pointer data)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pointer jack_get_ports(
    final Pointer client,
    final String port_name_pattern,
    final String type_name_pattern,
    final long flags)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void jack_free(final Pointer pointer)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String jack_port_name(final Pointer port)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String jack_port_short_name(final Pointer pointer)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String jack_port_type(final Pointer pointer)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_port_flags(final Pointer pointer)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean jack_port_is_mine(
    final Pointer client,
    final Pointer pointer)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pointer jack_port_get_buffer(
    final Pointer port,
    final int frames)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_port_name_size()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pointer jack_port_by_name(
    final Pointer client,
    final String name)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_activate(final Pointer client)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_connect(
    final Pointer client,
    final String source_port,
    final String target_port)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_disconnect(
    final Pointer client,
    final String source_port,
    final String target_port)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jack_deactivate(final Pointer client)
  {
    throw new UnsupportedOperationException();
  }
}
