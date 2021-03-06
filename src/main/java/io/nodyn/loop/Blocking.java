/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nodyn.loop;

import io.netty.channel.EventLoopGroup;


/**
 * @author Bob McWhirter
 */
public class Blocking {

    private final ManagedEventLoopGroup managedLoop;

    public Blocking(ManagedEventLoopGroup managedLoop) {
        this.managedLoop = managedLoop;
    }


    public void submit(final Runnable action) {
        final RefHandle handle = this.managedLoop.newHandle();
        new Thread(new Runnable() {
            @Override
            public void run() {
                action.run();
                handle.unref();
            }
        }).start();
    }

    public void unblock(final Runnable action) {
        final EventLoopGroup elg = managedLoop.getEventLoopGroup();
        final RefHandle refHandle = managedLoop.newHandle();
        elg.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    action.run();
                } finally {
                    refHandle.unref();
                }

            }
        });
    }


}
