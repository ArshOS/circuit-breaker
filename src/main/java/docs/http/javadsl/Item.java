package docs.http.javadsl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

    final String name;
    String quality = "";

    @JsonCreator
    Item(@JsonProperty("name") String name) {
        this.name = randomNumberGenerator(name);
    }

    public String randomNumberGenerator(String name) {
        double val = Math.random() * 10;
        if (name.equals("VideoStream")) {
            if (val < 4) {
                quality = "Poor";
                return "Poor";
            } else if (val < 7) {
                quality = "Good";
                return "Good";
            } else {
                quality = "Very Good";
                return "Very Good";
            }
        } else if (name.equals("AudioStream")) {
            if (val < 2) {
                quality = "Poor";
                return "Poor";
            } else if (val < 6) {
                quality = "Good";
                return "Good";
            } else {
                quality = "Very Good";
                return "Very Good";
            }
        } else if (name.equals("ChatStream")){
            if (val < 0.1) {
                quality = "Poor";
                return "Poor";
            } else if (val < 1) {
                quality = "Good";
                return "Good";
            } else {
                quality = "Very Good";
                return "Very Good";
            }
        } else {
            return name;
        }
    }

    public String getName() {
        return name;
    }
}
