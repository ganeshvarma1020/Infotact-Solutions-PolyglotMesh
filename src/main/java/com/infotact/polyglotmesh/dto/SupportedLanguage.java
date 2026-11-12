package com.infotact.polyglotmesh.dto;

public enum SupportedLanguage {
    JS("js", "JavaScript (GraalJS ECMAScript 2023)"),
    PYTHON("python", "Python 3 (GraalPy)");

    private final String graalId;
    private final String displayName;

    SupportedLanguage(String graalId, String displayName) {
        this.graalId = graalId;
        this.displayName = displayName;
    }

    public String getGraalId() { return graalId; }
    public String getDisplayName() { return displayName; }

    public static SupportedLanguage fromString(String value) {
        for (SupportedLanguage lang : values()) {
            if (lang.graalId.equalsIgnoreCase(value) ||
                lang.name().equalsIgnoreCase(value)) return lang;
        }
        throw new IllegalArgumentException("Unsupported guest language: " + value
            + ". Allowed: js, python");
    }
}
