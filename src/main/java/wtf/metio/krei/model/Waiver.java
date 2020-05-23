package wtf.metio.krei.model;

import wtf.metio.krei.template.waiver.Metio;

public enum Waiver {

    METIO("metio", Metio.TEXT);

    private final String id;
    private final String text;

    Waiver(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String id() {
        return id;
    }

    public String text() {
        return text;
    }
}
