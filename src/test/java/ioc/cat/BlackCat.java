package ioc.cat;

import com.heybcat.tightlyweb.ioc.annotation.Cat;

@Cat
public class BlackCat {

    public BlackCat()
    {
        System.out.println("black cat init");
    }

    public void eat()
    {
        System.out.println("black cat eat");
    }

}
