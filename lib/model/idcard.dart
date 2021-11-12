// To parse this JSON data, do
//
//     final idCardFontInfo = idCardFontInfoFromMap(jsonString);

import 'dart:convert';

class IdCardFontInfo {
  IdCardFontInfo({
    this.result,
    this.image,
    this.name,
    this.address,
    this.birthday,
    this.idNumber,
    this.ethnic,
    this.gender,
  });

  bool? result;
  String? image;
  String? name;
  String? address;
  String? birthday;
  String? idNumber;
  String? ethnic;
  String? gender;

  factory IdCardFontInfo.fromJson(String str) => IdCardFontInfo.fromMap(json.decode(str));

  String toJson() => json.encode(toMap());

  factory IdCardFontInfo.fromMap(Map<dynamic, dynamic> json) => IdCardFontInfo(
    result: json["result"],
    image: json["image"],
    name: json["name"],
    address: json["address"],
    birthday: json["birthday"],
    idNumber: json["IdNumber"],
    ethnic: json["ethnic"],
    gender: json["gender"],
  );

  Map<String, dynamic> toMap() => {
    "result": result,
    "image": image,
    "name": name,
    "address": address,
    "birthday": birthday,
    "IdNumber": idNumber,
    "ethnic": ethnic,
    "gender": gender,
  };
}

class IdCardBackInfo {
  IdCardBackInfo({
    this.result,
    this.image,
    this.signDate,
    this.expiryDate,
    this.issueAuthority,
  });

  bool? result;
  String? image;
  String? signDate;
  String? expiryDate;
  String? issueAuthority;

  factory IdCardBackInfo.fromJson(String str) => IdCardBackInfo.fromMap(json.decode(str));

  String toJson() => json.encode(toMap());

  factory IdCardBackInfo.fromMap(Map<dynamic, dynamic> json) => IdCardBackInfo(
    result: json["result"],
    image: json["image"],
    signDate: json["signDate"],
    expiryDate: json["expiryDate"],
    issueAuthority: json["issueAuthority"],
  );

  Map<String, dynamic> toMap() => {
    "result": result,
    "image": image,
    "signDate": signDate,
    "expiryDate": expiryDate,
    "issueAuthority": issueAuthority,
  };
}