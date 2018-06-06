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

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_INPUT;
import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_OUTPUT;

/**
 * A connection to a JACK server.
 */

public interface JackClientType extends AutoCloseable
{
  /**
   * @return The full name of the client
   *
   * @see "jack_get_client_name"
   */

  String name();

  /**
   * Activate the client.
   *
   * @throws JackException On errors
   * @see "jack_activate"
   */

  void activate()
    throws JackException;

  /**
   * @return {@code true} if the client is active
   *
   * @see #deactivate()
   * @see #activate()
   */

  boolean isActive();

  /**
   * Deactivate the client.
   *
   * @throws JackException On errors
   * @see "jack_deactivate"
   */

  void deactivate()
    throws JackException;

  /**
   * Retrieve the current maximum size that will ever be passed to the process
   * callback. It should only be used *before* the client has been activated.
   * This size may change, clients that depend on it must register a
   * callback so they will be notified if it does.
   *
   * @return The current maximum size
   *
   * @throws JackException On errors
   * @see "jack_get_buffer_size"
   */

  int bufferSize()
    throws JackException;

  /**
   * Retrieve the sample rate of the jack system, as set by the user when
   * {@code jackd} was started.
   *
   * @return The current sample rate
   *
   * @throws JackException On errors
   * @see "jack_get_sample_rate"
   */

  int sampleRate()
    throws JackException;

  /**
   * Retrieve the current CPU load estimated by JACK. This is a running average
   * of the time it takes to execute a full process cycle for all clients as a
   * percentage of the real time available per cycle determined by the buffer
   * size and sample rate.
   *
   * @return The current CPU load
   *
   * @throws JackException On errors
   * @see "jack_cpu_load"
   */

  float cpuLoad()
    throws JackException;

  /**
   * Set the process callback for the client.
   *
   * @param process The process callback
   *
   * @throws JackException On errors
   * @see "jack_set_process_callback"
   */

  void setProcessCallback(
    JackClientProcessCallbackType process)
    throws JackException;

  /**
   * Register a new port of the default type.
   *
   * @param name    The port name
   * @param options The port options
   *
   * @return A new port
   *
   * @throws JackException On errors
   * @see #portRegister(String, String, Set, long)
   * @see "jack_port_register"
   */

  default JackPortType portRegister(
    final String name,
    final Set<JackPortFlag> options)
    throws JackException
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(options, "options");
    return this.portRegister(name, "32 bit float mono audio", options, 0L);
  }

  /**
   * Create a new port for the client. This is an object used for moving data
   * of any type in or out of the client. Ports may be connected in various ways.
   * The port name must be unique among all ports owned by this client.
   * If the name is not unique, the registration will fail.
   *
   * All ports have a type, which may be any non-zero length string, passed as
   * an argument. Some port types are built into the JACK API,
   * like JACK_DEFAULT_AUDIO_TYPE or JACK_DEFAULT_MIDI_TYPE
   *
   * @param name        The port name
   * @param type        The port type
   * @param options     The port options
   * @param buffer_size The buffer size (must be non-zero if this is not a
   *                    built-in port type. Otherwise, it is ignored.)
   *
   * @return A new port
   *
   * @throws JackException On errors
   * @see "jack_port_register"
   */

  JackPortType portRegister(
    String name,
    String type,
    Set<JackPortFlag> options,
    long buffer_size)
    throws JackException;

  /**
   * Find ports matching the given parameters.
   *
   * @param name_pattern A regular expression used to select ports by name.
   *                     If empty, no selection based on name will be carried out.
   * @param type_pattern A regular expression used to select ports by type.
   *                     If empty, no selection based on type will be carried out.
   * @param flags        A set of flags used to select ports. If empty,
   *                     no selection based on flags will be carried out.
   *
   * @return A list of matching port names
   *
   * @throws JackException On errors
   * @see "jack_get_ports"
   */

  List<String> portsList(
    Optional<String> name_pattern,
    Optional<String> type_pattern,
    Set<JackPortFlag> flags)
    throws JackException;

  /**
   * Lists all input ports.
   *
   * @return A list of ports
   *
   * @throws JackException On errors
   * @see #portsList(Optional, Optional, Set)
   */

  default List<String> portsListAllInputs()
    throws JackException
  {
    return this.portsList(
      Optional.empty(),
      Optional.empty(),
      EnumSet.of(JACK_PORT_IS_INPUT));
  }

  /**
   * Lists all output ports.
   *
   * @return A list of ports
   *
   * @throws JackException On errors
   * @see #portsList(Optional, Optional, Set)
   */

  default List<String> portsListAllOutputs()
    throws JackException
  {
    return this.portsList(
      Optional.empty(),
      Optional.empty(),
      EnumSet.of(JACK_PORT_IS_OUTPUT));
  }

  /**
   * Establish a connection between two ports.
   *
   * When a connection exists, data written to the source port will be available
   * to be read at the destination port.
   *
   * @param source_port The source port
   * @param target_port The target port
   *
   * @return {@code true} iff a connection was created. If a connection already
   * exists, {@code false} is returned.
   *
   * @throws JackException On errors
   * @see "jack_connect"
   */

  boolean portsConnect(
    String source_port,
    String target_port)
    throws JackException;

  /**
   * Remove a connection between two ports.
   *
   * @param source_port The source port
   * @param target_port The target port
   *
   * @throws JackException On errors
   * @see "jack_disconnect"
   */

  void portsDisconnect(
    String source_port,
    String target_port)
    throws JackException;

  /**
   * Find a port matching the given name.
   *
   * @param name The port name
   *
   * @return The port, if any
   *
   * @throws JackException On errors
   * @see "jack_port_by_name"
   */

  Optional<JackPortType> portByName(
    String name)
    throws JackException;

  /**
   * Close the client. After this method is called, any attempt to call any
   * other methods (except {@link #isClosed()}) on this client instance will
   * raise an exception.
   *
   * @throws JackException On errors
   * @see "jack_client_close"
   */

  @Override
  void close()
    throws JackException;

  /**
   * @return {@code true} iff the client has been closed
   *
   * @see #close()
   */

  boolean isClosed();
}
