# Structure of this Project

## buildscripts

JS build scipts to transpile components from vuejs or react

## core

### core / commons

A bundle providing common code to all modules

## core / base

per:cms base - provides all peregrine specific node types and models as well as the 
basic authentication configuration for the apache sling repository

## core / login

fragment providing a customized login for apache sling

## core / felib

felib provides the ability to bundle multiple js and css files into single files

## core / node-js

addition of node-js into apache sling

## core / distribution

configuration and implementation of default multi instance distribution for sling instances

## pagerenderers

base page renderes for different site rendering approaches

### pagerenderers / vue

implementation of vuejs based site rendering

### pagerenderers / react

implementation of react based site rendering

### pagerenderers / static

implementation of server side based site rendering using HTL

## admin

### admin / base
administration console for per:cms

### admin / sites

### admin / assets

### admin / objects

## samples

sample projects to help explore per:cms

### samples / example-vue-site

sample site implemented with the vuejs page rendering mechanism

### samples / blog

sample blog site implemented based on vuejs page rendering

## testing

server side testing suite

## tooling

tooling for per:cms

### tooling / maven / archetypes / projects

project archetype for per:cms to easily create new projects

##resources

the currently supported apache sling version with all the dependencies needed for per:cms
