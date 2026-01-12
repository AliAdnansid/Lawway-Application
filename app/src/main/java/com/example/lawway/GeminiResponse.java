package com.example.lawway;

import com.google.gson.annotations.SerializedName;

public class GeminiResponse {
    @SerializedName("candidates")
    private Candidate[] candidates;

    public Candidate[] getCandidates() {
        return candidates;
    }

    public void setCandidates(Candidate[] candidates) {
        this.candidates = candidates;
    }

    public static class Candidate {
        @SerializedName("content")
        private Content content;

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }
    }

    public static class Content {
        @SerializedName("parts")
        private Part[] parts;

        public Part[] getParts() {
            return parts;
        }

        public void setParts(Part[] parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
