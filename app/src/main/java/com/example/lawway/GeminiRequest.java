package com.example.lawway;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {
    private List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = new ArrayList<>();
        Content.Part part = new Content.Part(text);
        Content content = new Content();
        content.parts = new ArrayList<>();
        content.parts.add(part);
        this.contents.add(content);
    }

    public List<Content> getContents() {
        return contents;
    }

    public static class Content {
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }

        public static class Part {
            private String text;

            public Part(String text) {
                this.text = text;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }
    }
}
