package com.qiuguan.mq.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author qiuguan
 * @version .java, v 0.1 2022/04/19  14:59:27 qiuguan Exp $
 */
@AllArgsConstructor
@ToString
@Setter
@Getter
public class Student implements Serializable {

    private Long id;

    private String name;

}
