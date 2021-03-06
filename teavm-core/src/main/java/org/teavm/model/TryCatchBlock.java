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
package org.teavm.model;

/**
 *
 * @author Alexey Andreev
 */
public class TryCatchBlock implements TryCatchBlockReader {
    BasicBlock protectedBlock;
    private BasicBlock handler;
    private String exceptionType;
    private Variable exceptionVariable;

    @Override
    public BasicBlock getHandler() {
        return handler;
    }

    public void setHandler(BasicBlock handler) {
        this.handler = handler;
    }

    @Override
    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public Variable getExceptionVariable() {
        return exceptionVariable;
    }

    public void setExceptionVariable(Variable exceptionVariable) {
        this.exceptionVariable = exceptionVariable;
    }

    @Override
    public BasicBlock getProtectedBlock() {
        return protectedBlock;
    }
}
