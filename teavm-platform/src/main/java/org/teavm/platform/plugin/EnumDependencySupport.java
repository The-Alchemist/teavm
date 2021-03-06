/*
 *  Copyright 2014 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.platform.plugin;

import org.teavm.dependency.AbstractDependencyListener;
import org.teavm.dependency.DependencyAgent;
import org.teavm.dependency.DependencyConsumer;
import org.teavm.dependency.DependencyNode;
import org.teavm.dependency.DependencyType;
import org.teavm.dependency.MethodDependency;
import org.teavm.model.CallLocation;
import org.teavm.model.ClassReader;
import org.teavm.model.MethodDescriptor;
import org.teavm.model.MethodReader;
import org.teavm.model.MethodReference;
import org.teavm.model.ValueType;
import org.teavm.platform.Platform;

/**
 *
 * @author Alexey Andreev
 */
public class EnumDependencySupport extends AbstractDependencyListener {
    private DependencyNode allEnums;

    @Override
    public void started(DependencyAgent agent) {
        allEnums = agent.createNode();
    }

    @Override
    public void classReached(DependencyAgent agent, String className, CallLocation location) {
        ClassReader cls = agent.getClassSource().get(className);
        if (cls == null || cls.getParent() == null || !cls.getParent().equals("java.lang.Enum")) {
            return;
        }
        allEnums.propagate(agent.getType(className));
    }

    @Override
    public void methodReached(final DependencyAgent agent, MethodDependency method, CallLocation location) {
        if (method.getReference().getClassName().equals(Platform.class.getName()) &&
                method.getReference().getName().equals("getEnumConstants")) {
            allEnums.connect(method.getResult().getArrayItem());
            final MethodReference ref = method.getReference();
            allEnums.addConsumer(new DependencyConsumer() {
                @Override public void consume(DependencyType type) {
                    ClassReader cls = agent.getClassSource().get(type.getName());
                    MethodReader method = cls.getMethod(new MethodDescriptor("values",
                            ValueType.arrayOf(ValueType.object(cls.getName()))));
                    if (method != null) {
                        agent.linkMethod(method.getReference(), new CallLocation(ref)).use();
                    }
                }
            });
            method.getResult().propagate(agent.getType("[java.lang.Enum"));
            for (String cls : agent.getAchievableClasses()) {
                classReached(agent, cls, location);
            }
        }
    }
}
