/*
 *  Copyright 2015 Alexey Andreev.
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
package org.teavm.jso.plugin;

import java.io.IOException;
import java.util.Map;
import org.teavm.codegen.SourceWriter;
import org.teavm.javascript.RenderingContext;
import org.teavm.jso.plugin.JSODependencyListener.ExposedClass;
import org.teavm.model.MethodDescriptor;
import org.teavm.vm.BuildTarget;
import org.teavm.vm.spi.RendererListener;

/**
 *
 * @author Alexey Andreev
 */
class JSOAliasRenderer implements RendererListener {
    private static String variableChars = "abcdefghijklmnopqrstuvwxyz";
    private JSODependencyListener dependencyListener;
    private SourceWriter writer;

    public JSOAliasRenderer(JSODependencyListener dependencyListener) {
        this.dependencyListener = dependencyListener;
    }

    @Override
    public void begin(RenderingContext context, BuildTarget buildTarget) throws IOException {
        writer = context.getWriter();
    }

    @Override
    public void complete() throws IOException {
        if (!dependencyListener.isAnyAliasExists()) {
            return;
        }

        writer.append("(function()").ws().append("{").softNewLine().indent();
        writer.append("var c;").softNewLine();
        for (Map.Entry<String, ExposedClass> entry : dependencyListener.getExposedClasses().entrySet()) {
            ExposedClass cls = entry.getValue();
            if (cls.methods.isEmpty()) {
                continue;
            }
            writer.append("c").ws().append("=").ws().appendClass(entry.getKey()).append(".prototype;").softNewLine();
            for (Map.Entry<MethodDescriptor, String> aliasEntry : cls.methods.entrySet()) {
                writer.append("c.").append(aliasEntry.getValue()).ws().append("=").ws().append("c.")
                        .appendMethod(aliasEntry.getKey()).append(";").softNewLine();
            }

            if (cls.functorField != null) {
                writeFunctor(cls);
            }
        }
        writer.outdent().append("})();").newLine();
    }

    private void writeFunctor(ExposedClass cls) throws IOException {
        String alias = cls.methods.get(cls.functorMethod);
        if (alias == null) {
            return;
        }

        writer.append("c.jso$functor$").append(alias).ws().append("=").ws().append("function()").ws().append("{")
                .indent().softNewLine();
        writer.append("if").ws().append("(!this.").appendField(cls.functorField).append(")").ws().append("{")
                .indent().softNewLine();
        writer.append("var self").ws().append('=').ws().append("this;").softNewLine();

        writer.append("this.").appendField(cls.functorField).ws().append('=').ws().append("function(");
        appendArguments(cls.functorMethod.parameterCount());
        writer.append(")").ws().append('{').indent().softNewLine();
        writer.append("return self.").appendMethod(cls.functorMethod).append('(');
        appendArguments(cls.functorMethod.parameterCount());
        writer.append(");").softNewLine();
        writer.outdent().append("};").softNewLine();

        writer.outdent().append("}").softNewLine();
        writer.append("return this.").appendField(cls.functorField).append(';').softNewLine();
        writer.outdent().append("};").softNewLine();
    }

    private void appendArguments(int count) throws IOException {
        for (int i = 0; i < count; ++i) {
            if (i > 0) {
                writer.append(',').ws();
            }
            writer.append(variableName(i));
        }
    }

    private String variableName(int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(variableChars.charAt(index % variableChars.length()));
        index /= variableChars.length();
        if (index > 0) {
            sb.append(index);
        }
        return sb.toString();
    }
}
