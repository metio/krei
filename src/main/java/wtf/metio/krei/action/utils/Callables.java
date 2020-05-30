/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.metio.krei.model.Action;
import wtf.metio.krei.model.Call;

import java.util.concurrent.Callable;

public final class Callables {

    private static final Logger LOGGER = LoggerFactory.getLogger(Callables.class);

    public static Action call(final Callable<?> callable) {
        return none(() -> callable.call());
    }

    public static Action none(final Call callable) {
        return () -> {
            try {
                callable.call();
                return 0;
            } catch (final Exception exception) {
                LOGGER.error("call failed with:", exception);
                return 1000;
            }
        };
    }

    private Callables() {
        // factory class
    }

}
