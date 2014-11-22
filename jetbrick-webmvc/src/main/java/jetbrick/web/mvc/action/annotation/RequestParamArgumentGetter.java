/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 *   Author: Guoqiang Chen
 *    Email: subchen@gmail.com
 *   WebURL: https://github.com/subchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.web.mvc.action.annotation;

import jetbrick.typecast.Convertor;
import jetbrick.typecast.TypeCastUtils;
import jetbrick.util.ArrayUtils;
import jetbrick.util.StringUtils;
import jetbrick.util.annotation.ValueConstants;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.WebConfig;

public final class RequestParamArgumentGetter implements AnnotatedArgumentGetter<RequestParam, Object> {

    private AbstractRequestParamGetter proxy;

    @Override
    public void initialize(ArgumentContext<RequestParam> ctx) {
        Class<?> type = ctx.getRawParameterType();

        if (type == String.class) {
            proxy = new BasicRequestParamGetter();
        } else if (type.isArray()) {
            proxy = new ArrayRequestParamGetter();
            ((ArrayRequestParamGetter) proxy).elementType = type.getComponentType();
        } else {
            RequestParamGetter<?> requestParamGetter = WebConfig.getRequestParamGetterResolver().resolve(type);
            if (requestParamGetter == null) {
                proxy = new BasicRequestParamGetter();
                ((BasicRequestParamGetter) proxy).cast = ctx.getTypeConvertor();
            } else {
                proxy = new CustomizedRequestParamGetter();
                ((CustomizedRequestParamGetter) proxy).requestParamGetter = requestParamGetter;
            }
        }

        RequestParam annotation = ctx.getAnnotation();
        String name = annotation.value();
        if (ValueConstants.isEmptyOrNull(name)) {
            name = ctx.getParameterName();
        }

        proxy.name = name;
        proxy.required = annotation.required();
        proxy.defaultValue = ValueConstants.trimToNull(annotation.defaultValue());
    }

    @Override
    public Object get(RequestContext ctx) throws Exception {
        return proxy.get(ctx);
    }

    static abstract class AbstractRequestParamGetter implements AnnotatedArgumentGetter<RequestParam, Object> {
        protected String name;
        protected boolean required;
        protected String defaultValue;

        @Override
        public void initialize(ArgumentContext<RequestParam> ctx) {
        }
    }

    static final class BasicRequestParamGetter extends AbstractRequestParamGetter {
        protected Convertor<?> cast;

        @Override
        public Object get(RequestContext ctx) throws Exception {
            String value = ctx.getParameter(name);
            if (value == null) {
                value = defaultValue;
            }

            if (value == null) {
                if (required) {
                    throw new IllegalStateException("request parameter is not found: " + name);
                }
                return null;
            }

            if (cast != null) {
                return cast.convert(value);
            }

            return value;
        }
    }

    static final class ArrayRequestParamGetter extends AbstractRequestParamGetter {
        protected Class<?> elementType;

        @Override
        public Object get(RequestContext ctx) throws Exception {
            String[] values = ctx.getParameterValues(name);
            if (values == null) {
                if (defaultValue != null) {
                    values = StringUtils.split(defaultValue, ',');
                }
            }

            if (values == null) {
                //if (required) {
                //    throw new IllegalStateException("request parameter is not found: " + name);
                //}
                values = ArrayUtils.EMPTY_STRING_ARRAY;
            }

            return TypeCastUtils.convertToArray(values, elementType);
        }
    }

    static final class CustomizedRequestParamGetter extends AbstractRequestParamGetter {
        protected RequestParamGetter<?> requestParamGetter;

        @Override
        public Object get(RequestContext ctx) throws Exception {
            Object value = requestParamGetter.get(ctx, name);
            if (value == null && required) {
                throw new IllegalStateException("request parameter is not found: " + name);
            }
            return value;
        }
    }
}
