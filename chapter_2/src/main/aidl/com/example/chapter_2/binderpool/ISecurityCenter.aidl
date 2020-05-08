package com.example.chapter_2.binderpool;

 interface ISecurityCenter {
     String encrypt(String content);
     String decrypt(String password);
 }