package com.tuempresa.tracking.dto;

public class MetaEventDTO {
    private String event_name;
    private Long event_time;
    private String action_source;
    private UserData user_data;
    private CustomData custom_data;

    // Getters y Setters
    public String getEvent_name() { return event_name; }
    public void setEvent_name(String event_name) { this.event_name = event_name; }

    public Long getEvent_time() { return event_time; }
    public void setEvent_time(Long event_time) { this.event_time = event_time; }

    public String getAction_source() { return action_source; }
    public void setAction_source(String action_source) { this.action_source = action_source; }

    public UserData getUser_data() { return user_data; }
    public void setUser_data(UserData user_data) { this.user_data = user_data; }

    public CustomData getCustom_data() { return custom_data; }
    public void setCustom_data(CustomData custom_data) { this.custom_data = custom_data; }

    public static class UserData {
        private String em;
        private String client_ip_address;
        private String client_user_agent;
        private String fbc;
        // Getters y Setters
        public String getEm() { return em; }
        public void setEm(String em) { this.em = em; }

        public String getClient_ip_address() { return client_ip_address; }
        public void setClient_ip_address(String client_ip_address) { this.client_ip_address = client_ip_address; }

        public String getClient_user_agent() { return client_user_agent; }
        public void setClient_user_agent(String client_user_agent) { this.client_user_agent = client_user_agent; }

        public String getFbc() { return fbc; }
        public void setFbc(String fbc) { this.fbc = fbc; }
    }

    public static class CustomData {
        private Double value;
        private String currency;

        // Getters y Setters
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}
