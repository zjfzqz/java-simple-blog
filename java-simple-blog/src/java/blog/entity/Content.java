/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.entity;

import blog.system.annotation.Bind;
import blog.system.loader.Load;
import blog.validation.annotation.NotEmpty;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

/**
 *
 * @author petroff
 */
public class Content {

    @Bind
    @NotEmpty
    private int id;

    @Bind
    private String text;

    private String lang;

    private int object_id;

    private int user_id;

    private String type;

    private static String errorMessage = "";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getObject_id() {
        return object_id;
    }

    public void setObject_id(int object_id) {
        this.object_id = object_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public static boolean validate(Object object, Validator validator) {
        return true;
    }

    public static String getErrorMessage() {
        return errorMessage;
    }

    public static void setErrorMessage(String errorMessage) {
        Content.errorMessage = errorMessage;
    }

}
