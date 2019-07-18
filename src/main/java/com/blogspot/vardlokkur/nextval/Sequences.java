package com.blogspot.vardlokkur.nextval;

import java.sql.SQLException;

/**
 * @author Warlock
 */
public interface Sequences {

    Integer nextValue(String sequenceName) throws SQLException;

}
