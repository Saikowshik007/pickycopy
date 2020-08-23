package com.example.pickycopy;

    public class CUser {
        String name;
        String phone;
        String email;
        String userId;
        String docId;
        String token;
        public CUser(String name, String phone, String email,String userId,String token) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.userId = userId;
            this.docId=docId;
            this.token=token;
        }

        public String getName() {
            return name;
        }
        public String getPhone() {
            return phone;
        }
        public String getEmail() {
            return email;
        }
        public String getUserId() { return userId;}
        public String token() { return token;}

    }

