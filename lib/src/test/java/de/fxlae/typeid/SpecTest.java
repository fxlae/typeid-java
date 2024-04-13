package de.fxlae.typeid;

class SpecTest extends AbstractSpecTest {

    @Override
    TypeIdStaticContext createStaticFacade() {
        return new TypeIdFacade();
    }
}