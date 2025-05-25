package org.glue.glue_be.notification.reminder;


import org.springframework.context.annotation.*;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

    private final AutowiringSpringBeanJobFactory jobFactory;

    public QuartzConfig(AutowiringSpringBeanJobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        return factory;
    }
}


