/*
 * Copyright 2016 John Grosh (jagrosh).
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
package com.jagrosh.jagtag;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * An object for parsing JagTag code
 *
 * @author John Grosh (jagrosh)
 */
public class Parser {
    private final ExecutorService parserThread = Executors.newSingleThreadExecutor((r) -> {
        final Thread t = new Thread(r, "jagtag parsing thread");
        t.setDaemon(true);
        return t;
    });
    private final HashMap<String, Method> methods;
    private final Environment environment;
    private final long iterations;
    private final int maxLength;
    private final int maxOutput;

    /**
     * Constructor for a JagTag parser. It is not recommended that you call
     * this; instead consider using JagTagParserBuilder#build() after adding
     * all the applicable methods
     *
     * @param methods
     *     - a collection of methods for the parser
     * @param iterations
     *     - the maximum number of iterations the parser will run
     * @param maxLength
     *     - the maximum internal length of the parse string
     * @param maxOutput
     *     - the maximum output of the parsed result
     */
    public Parser(Collection<Method> methods, long iterations, int maxLength, int maxOutput) {
        this.environment = new Environment();
        this.methods = new HashMap<>();
        methods.forEach((method) -> this.methods.put(method.getName(), method));
        this.iterations = iterations;
        this.maxLength = maxLength;
        this.maxOutput = maxOutput;
    }

    /**
     * Inserts objects to be used while parsing
     *
     * @param key
     *     - the name of the object
     * @param value
     *     - the value of the object
     *
     * @return the parser after the object has been added
     */
    public synchronized Parser put(String key, Object value) {
        environment.put(key, value);
        return this;
    }

    /**
     * Clears all the objects from the parser
     *
     * @return the parser after the objects have been cleared
     */
    public synchronized Parser clear() {
        environment.clear();
        return this;
    }

    /**
     * Parses a String of JagTag code asynchronously, utilizing the object that have been added
     * <p>This method takes the current parser environment and makes a copy of it resulting in a new env for the parse result</p>
     *
     * @param input The input to be parsed
     * @param callback The result of the parser, called on the parser thread
     */
    public void parseAsync(String input, Consumer<ParseResult> callback) {
        this.parserThread.submit(() -> {
            final Environment env = this.environment.copy();
            final String parsed = this.parse(input, env);

            callback.accept(new ParseResult(parsed, env));
        });
    }

    /**
     * Parses a String of JagTag code, utilizing the object that have been added
     *
     * @param input The input to be parsed
     *
     * @return the parsed String
     */
    public synchronized String parse(String input) {
        return this.parse(input, this.environment);
    }

    /**
     * Parses a String of JagTag code, utilizing the object that have been added
     *
     * @param input The input to be parsed
     * @param environment The environment to use
     *
     * @return the parsed String
     */
    // TODO: the contents of if-statements are parsed before the actual statement itself
    //  This should be fixed by making the parser start on the outside instead of the inside
    public synchronized String parse(String input, Environment environment) {
        String output = filterEscapes(input);
        int count = 0;
        String lastoutput = "";
        while (!lastoutput.equals(output) && count < iterations && output.length() <= maxLength) {
            lastoutput = output;
            int i1 = output.indexOf("}");
            int i2 = (i1 == -1 ? -1 : output.lastIndexOf("{", i1));
            if (i1 != -1 && i2 != -1)//otherwise, we're done
            {
                String contents = output.substring(i2 + 1, i1);
                String result = null;
                int split = contents.indexOf(":");
                if (split == -1) {
                    Method method = methods.get(contents.trim());
                    if (method != null)
                        try {
                            result = method.parseSimple(environment);
                        } catch (ParseException ex) {
                            return ex.getMessage();
                        }
                } else {
                    String name = contents.substring(0, split);
                    String params = contents.substring(split + 1);
                    Method method = methods.get(name.trim());
                    if (method != null)
                        try {
                            result = method.parseComplex(environment, defilterAll(params));
                        } catch (ParseException ex) {
                            return ex.getMessage();
                        }
                }
                if (result == null)
                    result = "{" + contents + "}";
                output = output.substring(0, i2) + filterAll(result) + output.substring(i1 + 1);
            }
            count++;
        }
        output = defilterAll(output);
        if (output.length() > maxOutput)
            output = output.substring(0, maxOutput);
        return output;
    }

    private static String filterEscapes(String input) {
        return input.replace("\\{", "\u0012").replace("\\|", "\u0013").replace("\\}", "\u0014");
    }

    private static String defilterEscapes(String input) {
        return input.replace("\u0012", "\\{").replace("\u0013", "\\|").replace("\u0014", "\\}");
    }

    private static String defilterAll(String input) {
        return defilterEscapes(input).replace("\u0015", "{").replace("\u0016", "}");
    }

    private static String filterAll(String input) {
        return filterEscapes(input).replace("{", "\u0015").replace("}", "\u0016");
    }
}
