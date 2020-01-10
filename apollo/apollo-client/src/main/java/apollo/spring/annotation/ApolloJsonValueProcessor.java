/*
 *    Copyright 2019-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package apollo.spring.annotation;

import apollo.build.ApolloInjector;
import apollo.spring.property.PlaceholderHelper;
import apollo.spring.property.SpringValue;
import apollo.spring.property.SpringValueRegistry;
import apollo.spring.util.SpringInjector;
import apollo.util.ConfigUtil;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Create by zhangzheng on 2018/2/6
 */
public class ApolloJsonValueProcessor extends ApolloProcessor implements BeanFactoryAware {

  private static final Logger logger = LoggerFactory.getLogger(ApolloJsonValueProcessor.class);
  private static final Gson gson = new Gson();

  private final ConfigUtil configUtil;
  private final PlaceholderHelper placeholderHelper;
  private final SpringValueRegistry springValueRegistry;
  private ConfigurableBeanFactory beanFactory;

  public ApolloJsonValueProcessor() {
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
  }

  @Override
  protected void processField(Object bean, String beanName, Field field) {
    ApolloJsonValue apolloJsonValue = AnnotationUtils.getAnnotation(field, ApolloJsonValue.class);
    if (apolloJsonValue == null) {
      return;
    }
    String placeholder = apolloJsonValue.value();
    Object propertyValue = placeholderHelper
        .resolvePropertyValue(beanFactory, beanName, placeholder);

    // propertyValue will never be null, as @ApolloJsonValue will not allow that
    if (!(propertyValue instanceof String)) {
      return;
    }

    boolean accessible = field.isAccessible();
    field.setAccessible(true);
    ReflectionUtils
        .setField(field, bean, parseJsonValue((String)propertyValue, field.getGenericType()));
    field.setAccessible(accessible);

    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);
      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, placeholder, bean, beanName, field, true);
        springValueRegistry.register(beanFactory, key, springValue);
        logger.debug("Monitoring {}", springValue);
      }
    }
  }

  @Override
  protected void processMethod(Object bean, String beanName, Method method) {
    ApolloJsonValue apolloJsonValue = AnnotationUtils.getAnnotation(method, ApolloJsonValue.class);
    if (apolloJsonValue == null) {
      return;
    }
    String placeHolder = apolloJsonValue.value();

    Object propertyValue = placeholderHelper
        .resolvePropertyValue(beanFactory, beanName, placeHolder);

    // propertyValue will never be null, as @ApolloJsonValue will not allow that
    if (!(propertyValue instanceof String)) {
      return;
    }

    Type[] types = method.getGenericParameterTypes();
    Preconditions.checkArgument(types.length == 1,
        "Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters",
        bean.getClass().getName(), method.getName(), method.getParameterTypes().length);

    boolean accessible = method.isAccessible();
    method.setAccessible(true);
    ReflectionUtils.invokeMethod(method, bean, parseJsonValue((String)propertyValue, types[0]));
    method.setAccessible(accessible);

    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeHolder);
      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, apolloJsonValue.value(), bean, beanName,
            method, true);
        springValueRegistry.register(beanFactory, key, springValue);
        logger.debug("Monitoring {}", springValue);
      }
    }
  }

  private Object parseJsonValue(String json, Type targetType) {
    try {
      return gson.fromJson(json, targetType);
    } catch (Throwable ex) {
      logger.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
      throw ex;
    }
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = (ConfigurableBeanFactory) beanFactory;
  }
}
