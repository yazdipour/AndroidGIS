package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import ir.gfpishro.geosuiteandroidprivateusers.Models.User;

public class MockObj {

    public static User user() {
        User user = new User();
        user.setCityCode("001");
        user.setFirstName("Shahriar");
        user.setId(1);
        user.setNationalCode("12345678");
        user.setPassword("1234");
        user.setPersonalId("0");
        return user;
    }
}