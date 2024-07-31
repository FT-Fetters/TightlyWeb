package com.heybcat.tightlyweb.common.ioc.invocation;

import com.heybcat.tightlyweb.common.ioc.IocManager;

/**
 * @author Fetters
 */
public interface BeforeInvocation {

    void invoke(IocManager iocManager);

}
