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

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Transient;
import jnr.ffi.types.u_int32_t;

/**
 * The libjack C API.
 */

// CHECKSTYLE:OFF
public interface LibJackType
{
  interface ProcessCallbackType
  {
    @Delegate
    int call(
      @u_int32_t int frames,
      Pointer data);
  }

  interface ShutdownCallbackType
  {
    @Delegate
    void call(
      Pointer data);
  }

  interface XRunCallbackType
  {
    @Delegate
    int call(
      Pointer data);
  }

  final class MidiEvent extends Struct
  {
    public final Unsigned32 frames = new Unsigned32();
    public final size_t size = new size_t();
    public final Pointer pointer = new Pointer();

    public MidiEvent(
      final Runtime runtime)
    {
      super(runtime);
    }
  }

  @IgnoreError
  Pointer jack_client_open(
    @In String name,
    @In int options,
    @Out int[] status,
    @In String server_name);

  @IgnoreError
  int jack_client_close(
    @In Pointer client);

  @IgnoreError
  String jack_get_client_name(
    @In Pointer client);

  @IgnoreError
  @u_int32_t
  int jack_get_sample_rate(
    @In Pointer client);

  @IgnoreError
  @u_int32_t
  int jack_get_buffer_size(
    @In Pointer client);

  @IgnoreError
  float jack_cpu_load(
    @In Pointer client);

  @IgnoreError
  int jack_set_process_callback(
    @In Pointer client,
    @In ProcessCallbackType process,
    @In Pointer data);

  @IgnoreError
  int jack_set_xrun_callback(
    @In Pointer client,
    @In XRunCallbackType process,
    @In Pointer data);

  @IgnoreError
  Pointer jack_port_register(
    @In Pointer client,
    @In String port_name,
    @In String port_type,
    long flags,
    long buffer_size);

  @IgnoreError
  void jack_on_shutdown(
    @In Pointer client,
    @In ShutdownCallbackType process,
    @In Pointer data);

  @IgnoreError
  Pointer jack_get_ports(
    @In Pointer client,
    @In String port_name_pattern,
    @In String type_name_pattern,
    long flags);

  @IgnoreError
  void jack_free(
    @In Pointer pointer);

  @IgnoreError
  String jack_port_name(
    @In Pointer port);

  @IgnoreError
  String jack_port_short_name(
    @In Pointer pointer);

  @IgnoreError
  String jack_port_type(
    @In Pointer pointer);

  @IgnoreError
  int jack_port_flags(
    @In Pointer pointer);

  @IgnoreError
  boolean jack_port_is_mine(
    @In Pointer client,
    @In Pointer pointer);

  @IgnoreError
  Pointer jack_port_get_buffer(
    @In Pointer port,
    @In @u_int32_t int frames);

  @IgnoreError
  int jack_port_name_size();

  @IgnoreError
  Pointer jack_port_by_name(
    @In Pointer client,
    @In String name);

  @IgnoreError
  int jack_activate(
    @In Pointer client);

  @IgnoreError
  int jack_connect(
    @In Pointer client,
    @In String source_port,
    @In String target_port);

  @IgnoreError
  int jack_disconnect(
    @In Pointer client,
    @In String source_port,
    @In String target_port);

  @IgnoreError
  int jack_deactivate(
    @In Pointer client);

  @IgnoreError
  @u_int32_t
  int jack_midi_get_event_count(
    @In Pointer buffer);

  @IgnoreError
  @u_int32_t
  int jack_midi_get_lost_event_count(
    @In Pointer buffer);

  @IgnoreError
  int jack_midi_event_get(
    @Out @Transient MidiEvent event,
    @In Pointer buffer,
    @In @u_int32_t int index);
}
