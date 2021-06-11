package com.jagrosh.jagtag;

public class ParseResult {
    public final String parsed;
    public final Environment environment;

    public ParseResult(String parsed, Environment environment) {
        this.parsed = parsed;
        this.environment = environment;
    }

    /**
     * Returns the parsed output
     * @return the parsed output
     */
    public String getParsed() {
        return parsed;
    }

    /**
     * Returns the environment consumed by the parser, this is possibly modified due to async parsing
     *
     * @return The environment used by the parser
     */
    public Environment getEnvironment() {
        return environment;
    }
}
