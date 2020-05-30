/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Units are the lowest level building blocks for project creation.
 */
@Value.Immutable
public interface Unit {

    //region Builders
    static ImmutableUnit.Builder builder() {
        return ImmutableUnit.builder();
    }
    
    static Unit noop() {
        return builder().id("urn:krei:noop").build();
    }
    //endregion

    /**
     * The unique ID of the unit.
     */
    String id();

    /**
     * A human readable name for the unit
     */
    Optional<String> description();

    /**
     * List of URIs referencing documentation for this unit or its configuration
     */
    List<String> documentation();

    /**
     * List of commands/arguments to execute.
     */
    List<String> exec();

    /**
     * Whether {@link #exec()} should be taken as-is (true), or whitespace delimited (false/default)
     */
    Optional<Boolean> execRaw();

    Optional<Action> action();

    /**
     * Units that are started as soon as this unit is started. Whether those units succeed does not affect the result of
     * this unit.
     */
    Set<Unit> wants();

    /**
     * Units that are started as soon as this unit is started. Whether those units succeed does affect the result of
     * this unit.
     */
    Set<Unit> requires();

    /**
     * Units that should already have been executed. In case they are not, this unit will fail immediately.
     */
    Set<Unit> requisite();

    /**
     * Configures negative requirement dependencies. If a unit has a Conflicts= setting on another unit, starting the
     * former will stop the latter and vice versa.
     */
    Set<Unit> conflicts();

    /**
     * If unit1 has the directive Before=unit2, then if both units are run, unit1 will be executed fully before unit2
     * starts.
     */
    Set<Unit> before();

    /**
     * If unit1 has the directive After=unit2, then if both units are run, unit2 will be executed fully before unit1
     * starts.
     */
    Set<Unit> after();

}

// https://www.freedesktop.org/software/systemd/man/systemd.unit.html
// -- asserts
// -- conditionals
// -- onSuccess/onFailure

/*
Valid unit names consist of a "name prefix" and a dot and a suffix specifying the unit type. The "unit prefix" must consist of one or more valid characters (ASCII letters, digits, ":", "-", "_", ".", and "\"). The total length of the unit name including the suffix must not exceed 256 characters. The type suffix must be one of ".service", ".socket", ".device", ".mount", ".automount", ".swap", ".target", ".path", ".timer", ".slice", or ".scope".
 */

/*
Units names can be parameterized by a single argument called the "instance name". The unit is then constructed based on a "template file" which serves as the definition of multiple services or other units. A template unit must have a single "@" at the end of the name (right before the type suffix). The name of the full unit is formed by inserting the instance name between "@" and the unit type suffix. In the unit file itself, the instance parameter may be referred to using "%i" and other specifiers, see below.
 */
