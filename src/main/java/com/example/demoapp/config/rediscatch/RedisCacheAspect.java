package com.example.demoapp.config.rediscatch;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisAOP
 * @description: rediscatch 切面缓存
 * @Aspect 定义切面：切面由切点和增强（引介）组成(可以包含多个切点和多个增强)，它既包括了横切逻辑的定义，也包括了连接点的定义，SpringAOP就是负责实施切面的框架，它将切面所定义的横切逻辑织入到切面所指定的链接点中。
 * @Pointcut 定义切点：切点是一组连接点的集合。AOP通过“切点”定位特定的连接点。通过数据库查询的概念来理解切点和连接点的关系再适合不过了：连接点相当于数据库中的记录，而切点相当于查询条件。
 * @Before ：在目标方法被调用之前做增强处理,@Before只需要指定切入点表达式即可。
 * @AfterReturning ： 在目标方法正常完成后做增强,@AfterReturning除了指定切入点表达式后，还可以指定一个返回值形参名returning,代表目标方法的返回值。
 * @Afterthrowing： 主要用来处理程序中未处理的异常,@AfterThrowing除了指定切入点表达式后，还可以指定一个throwing的返回值形参名,可以通过该形参名来访问目标方法中所抛出的异常对象。
 * @After： 在目标方法完成之后做增强，无论目标方法时候成功完成。@After可以指定一个切入点表达式。
 * @Around： 环绕通知, 在目标方法完成前后做增强处理, 环绕通知是最重要的通知类型, 像事务, 日志等都是环绕通知, 注意编程中核心是一个ProceedingJoinPoint。
 */
@Component
@Aspect
@Log4j2
public class RedisCacheAspect {

    //Redis 缓存时长 ,单位分钟
    private static final Integer TIME_OUT = 30;

    /**
     * redis模板
     */
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * @return void
     * @Title: queryCachePointcut
     * @Description: 定义切点为缓存注解
     **/
    @Pointcut("execution(public * com.example.demoapp.controller.*.*(..)) && @within(com.example.demoapp.config.rediscatch.RedisCache)")
    public void queryCachePointcut() {
        // 查了一下资料 ,发现这有两个注解  @within是对象级别 , @annotation 是方法级别  我特地试了一下 ,还确实是的
        // 对应的元注解中@Target(ElementType.TYPE)  类型也要匹配
        // @execution 定义扫描路径 , 不写应该是全局
        // Pointcut 可以对应多个
    }

    /**
     * before before 在目标方法被调用之前做增强处理
     *
     * @param joinPoint
     */
    @Before("queryCachePointcut()")
    public void before(JoinPoint joinPoint) {
        // 在 around 中 , 调用目标方法之前 ,执行完成;
        // 不执行目标方法的话 , 无法触发before
        log.info("**********  Before  ***********");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("around 中调用方法了...," + method.getName());
        log.info("**********  Before End ***********");
    }

    /**
     * Around Around  环绕通知, 在目标方法完成前后做增强处理, 环绕通知是最重要的通知类型, 像事务, 日志等都是环绕通知
     *
     * @param joinPoint 注意是 ProceedingJoinPoint ProceedingJoinPoint ProceedingJoinPoint!!!
     * @return
     * @throws Throwable
     */
    @Around("queryCachePointcut()")
    public Object Interceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("********** Around ***********");
        // 方法开始时间
        long beginTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 类路径名
        String classPathName = joinPoint.getTarget().getClass().getName();
        // 类名
        String className = classPathName.substring(classPathName.lastIndexOf(".") + 1, classPathName.length());
        // 获取方法名
        String methodName = signature.getMethod().getName();
        // 参数名
        String[] strings = signature.getParameterNames();
        // redis缓存唯一值
        String key = className + "_" + methodName + "_" + Arrays.toString(strings);
        // 获取方法入参数
        ModelMap model = null;
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            model = (ModelMap) args[0];
        }
        log.info("********** Around End***********");
        // 调用原始方法
        return handlerData(model, className, methodName, beginTime, joinPoint, key);
    }

    /**
     * after after 在目标方法完成之后做增强
     *
     * @param joinPoint
     */
    @After("queryCachePointcut()")
    public void after(JoinPoint joinPoint) {
        // 在执行完 around之后 , 这个顺序我个人感觉很别扭 , before在around之中 ,目标方法调用之前
        // after却在 around 完成之后 ,而不是目标方法调用之后
        log.info("**********  After  ***********");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("around 方法执行完成了...," + method.getName());
        log.info("**********  After End ***********");
    }

    /**
     * AfterReturning AfterReturning  在目标方法正常完成后做增强
     *
     * @param joinPoint
     */
    @AfterReturning("queryCachePointcut()")
    public void AfterReturning(JoinPoint joinPoint) {
        // 正确完成目标方法后做处理  , 在after之后
        log.info("**********  AfterReturning  ***********");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("around || 目标方法 || after 正确方法执行完成了..." + method.getName());
        log.info("**********  AfterReturning End ***********");
    }

    /**
     * Afterthrowing Afterthrowing 主要用来处理程序中未处理的异常
     *
     * @param joinPoint
     */
    @AfterThrowing("queryCachePointcut()")
    public void Afterthrowing(JoinPoint joinPoint) {
        // 没有正确执行完成 , 程序存在未处理异常
        log.info("**********  Afterthrowing  ***********");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("around || 目标方法 || after || AfterReturning 方法出异常了" + method.getName());
        log.info("**********  Afterthrowing End ***********");
    }

    /**
     * 根据切换点的相关键值获取redis缓存数据
     *
     * @param beginTime
     * @param joinPoint
     * @param key
     * @return
     * @throws Throwable
     */
    private Object getObject(long beginTime, ProceedingJoinPoint joinPoint, String key) throws Throwable {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        // 判断是否存在
        boolean hasKey = redisTemplate.hasKey(key);
        Object object = null;
        if (hasKey) {
            // 缓存中获取到数据，直接返回。
            object = operations.get(key);
            log.warn("AOP 缓存切面处理 >>>> end 耗时：" + (System.currentTimeMillis() - beginTime));
            return object;
        }
        if (object == null) {
            ModelMap model = null;
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                model = (ModelMap) args[0];
            }
            // 缓存中没有数据，调用原始方法查询数据库
            joinPoint.proceed();
            operations.set(key, model.get("users"), TIME_OUT, TimeUnit.MINUTES); // 设置超时时间30分钟
            log.warn("向 Redis 添加 key 为 [" + key + " ] , 存活时长为 " + TIME_OUT + " min");
            log.warn("AOP 缓存切面处理 >>>> end 耗时：" + (System.currentTimeMillis() - beginTime));
        }
        return object;
    }

    /**
     * 根据目标方法相关信息生成redis缓存key值
     *
     * @return
     */
    private Object handlerData(ModelMap model, String className, String methodName, long beginTime, ProceedingJoinPoint joinPoint, String key) throws Throwable {
        if ((methodName.indexOf("select") != -1)) {
            // 如果为查询方法 , 直接去redis中获取数据 , 200w数据 查询接近快10倍
            // 去查了一下redis缓存的使用场景  看到可以缓存静态资源 ,后续可以研究一下
            Object data = getObject(beginTime, joinPoint, key);
            if (data != null) {
                // 跳转到列表页面 , 查看总数及查询耗时
                model.addAttribute("users", data);
                model.addAttribute("time", (System.currentTimeMillis() - beginTime));
                log.info("********** Around End***********");
                return "index.html";
            }
            log.info("********** Around End***********");
            return "index.html";
        } else if ((methodName.indexOf("add") != -1 || methodName.indexOf("delete") != -1 || methodName.indexOf("update") != -1)) {
            // 新增 修改 删除  移除redis缓存  , 等待下一次重新缓存
            // 查询 类名相关所有缓存
            Set<String> keys = redisTemplate.keys(className + "*");
            // 移除
            redisTemplate.delete(keys);
            log.warn("执行方法 : [ " + methodName + " ] :  清除 key 包含 [ " + className + " ] 的缓存数据");
            log.warn("AOP 缓存切面处理 >>>> end 耗时：" + (System.currentTimeMillis() - beginTime));
        }
        return joinPoint.proceed();
    }

    /**
     * redisTemplate 自动注入
     *
     * @param redisTemplate
     */
    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisSerializer stringSerializer = new StringRedisSerializer();//序列化为String
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);//序列化为Json
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        this.redisTemplate = redisTemplate;
    }

}
