/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.model;

import java.util.Date;

/**
 *
 * @author QuocBao
 */
public class AllowCommand {
    private Integer user_id;
    private String command_text;
    private Integer cmd_id;
    private Date create_at;
    private Boolean is_active;

    public AllowCommand() {
    }
     public AllowCommand(Integer user_id, String command_text, Integer cmd_id, Date create_at, Boolean is_active) {
        this.user_id = user_id;
        this.command_text = command_text;
        this.cmd_id = cmd_id;
        this.create_at = create_at;
        this.is_active = is_active;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getCommand_text() {
        return command_text;
    }

    public void setCommand_text(String command_text) {
        this.command_text = command_text;
    }

    public Integer getCmd_id() {
        return cmd_id;
    }

    public void setCmd_id(Integer cmd_id) {
        this.cmd_id = cmd_id;
    }

    public Date getCreate_at() {
        return create_at;
    }

    public void setCreate_at(Date create_at) {
        this.create_at = create_at;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }
    
    
}
