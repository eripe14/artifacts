package pl.karoldronia.artefacts.adventure;

import java.util.function.UnaryOperator;

public class LegacyColorPreProcessor implements UnaryOperator<String> {

    @Override
    public String apply(String component) {
        return component.replace("§", "&");
    }

}