package com.example.alerti_back.Model;

import java.util.Date;

public class UserReward {
    private int id;
    private String user_id; // Supabase utilise user_id comme VARCHAR
    private int reward_id;
    private int points_spent;
    private String status; // 'pending', 'claimed', 'expired'
    private Date claimed_at;
    private Date expires_at;
    private Date created_at;

    // Getters & Setters

    public int getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public int getReward_id() {
        return reward_id;
    }

    public int getPoints_spent() {
        return points_spent;
    }

    public String getStatus() {
        return status;
    }

    public Date getClaimed_at() {
        return claimed_at;
    }

    public Date getExpires_at() {
        return expires_at;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setReward_id(int reward_id) {
        this.reward_id = reward_id;
    }

    public void setPoints_spent(int points_spent) {
        this.points_spent = points_spent;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setClaimed_at(Date claimed_at) {
        this.claimed_at = claimed_at;
    }

    public void setExpires_at(Date expires_at) {
        this.expires_at = expires_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
