package com.example.pickycopy;

    public class CUser {
        String name;
        String phone;
        String email;
        String userId;
        String docId;
        String token;
        String address;
        boolean expanded;
        public CUser(String name, String phone, String email,String userId,String token,String address) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.userId = userId;
            this.docId=docId;
            this.token=token;
            this.address=address;
            this.expanded=false;
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
        public String getAddress(){return address;}
        public void setExpanded(boolean expanded){this.expanded=expanded;}
        public boolean isExpanded(){return expanded;}

    }

