package eu.cloudref.models;

public enum Role {
        MAINTAINER("MAINTAINER"), USER("USER");

        private String name;

        private Role(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
}