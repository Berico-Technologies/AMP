package amp.rabbit.dispatch;


import cmf.bus.IRegistration;


public interface IListenerCloseCallback {

    void onClose(IRegistration registration, RabbitListener listener);
}
