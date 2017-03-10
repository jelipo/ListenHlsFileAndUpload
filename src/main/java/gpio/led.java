package gpio;

import com.pi4j.io.gpio.*;

/**
 * Created by cao on 2017/3/10.
 */
public class led {

    final static GpioController gpio = GpioFactory.getInstance();
    // 获取1号GPIO针脚并设置高电平状态，对应的是树莓派上的12号针脚，可以参考pi4j提供的图片。
    final static GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "LED", PinState.HIGH);

    private static void main(String[] args) throws InterruptedException {
        System.out.println("开/关");
        // 创建一个GPIO控制器
        while (true){
            pin.toggle();
            Thread.sleep(2000);
        }

    }
}
