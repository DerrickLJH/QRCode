package com.example.derrick.qrcode;

public class Event {
    private int event_id;
    private String event_name;

    public Event(int event_id, String event_name) {
        this.event_id = event_id;
        this.event_name = event_name;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    @Override
    public String toString() {
        return "event_id=112233&event_name=SOI FYP Project Briefing";
    }
}
