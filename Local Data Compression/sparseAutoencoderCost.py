# -*- coding: utf-8 -*-
"""
Created on Mon Sep 16 22:24:04 2013

@author: puaykai
"""

from __future__ import division
import numpy as np
from numpy import linalg as LA
from numpy import dot
from array import array
from display import displayNetwork as dn

def sparseAutoencoderCost(theta, visibleSize, hiddenSize, lambd, sparsityParam, beta, data):
    
    """
    % visibleSize: the number of input units (probably 64) 
    % hiddenSize: the number of hidden units (probably 25) 
    % lambda: weight decay parameter
    % sparsityParam: The desired average activation for the hidden units (denoted in the lecture
    %                           notes by the greek alphabet rho, which looks like a lower-case "p").
    % beta: weight of sparsity penalty term
    % data: Our 64x10000 matrix containing the training data.  So, data(:,i) is the i-th training example. 
      
    % The input theta is a vector (because minFunc expects the parameters to be a vector). 
    % We first convert theta to the (W1, W2, b1, b2) matrix/vector format, so that this 
    % follows the notation convention of the lecture notes. 
    """
    """    
    W1 = np.matrix(np.reshape(theta[0,0:hiddenSize*visibleSize], (hiddenSize, visibleSize)))
    W2 = np.matrix(np.reshape(theta[0,hiddenSize*visibleSize:2*hiddenSize*visibleSize], (visibleSize,hiddenSize)))
    b1 = np.matrix(theta[0,2*hiddenSize*visibleSize:2*hiddenSize*visibleSize+hiddenSize])
    b2 = np.matrix(theta[0,2*hiddenSize*visibleSize+hiddenSize:])
    """
    W1 = np.reshape(theta[0:hiddenSize*visibleSize], ( visibleSize,hiddenSize))
    W2 = np.reshape(theta[hiddenSize*visibleSize:2*hiddenSize*visibleSize], (hiddenSize,visibleSize))
    b1 = theta[2*hiddenSize*visibleSize:2*hiddenSize*visibleSize+hiddenSize]
    b2 = theta[2*hiddenSize*visibleSize+hiddenSize:]
    
    
    #Cost and gradient variables (your code needs to compute these values). 
    #Here, we initialize them to zeros.
    cost = 0
    W1grad = np.zeros(W1.shape)
    W2grad = np.zeros(W2.shape)
    b1grad = np.zeros(b1.shape)
    b2grad = np.zeros(b2.shape)
    
    """
    %% ---------- YOUR CODE HERE --------------------------------------
    %  Instructions: Compute the cost/optimization objective J_sparse(W,b) for the Sparse Autoencoder,
    %                and the corresponding gradients W1grad, W2grad, b1grad, b2grad.
    %
    % W1grad, W2grad, b1grad and b2grad should be computed using backpropagation.
    % Note that W1grad has the same dimensions as W1, b1grad has the same dimensions
    % as b1, etc.  Your code should set W1grad to be the partial derivative of J_sparse(W,b) with
    % respect to W1.  I.e., W1grad(i,j) should be the partial derivative of J_sparse(W,b) 
    % with respect to the input parameter W1(i,j).  Thus, W1grad should be equal to the term 
    % [(1/m) \Delta W^{(1)} + \lambda W^{(1)}] in the last block of pseudo-code in Section 2.2 
    % of the lecture notes (and similarly for W2grad, b1grad, b2grad).
    % 
    % Stated differently, if we were using batch gradient descent to optimize the parameters,
    % the gradient descent update to W1 would be W1 := W1 - alpha * W1grad, and similarly for W2, b1, b2. 
    % 
    """
    
    datasize = data.shape
    number_of_patches = datasize[0] #number of columns
    
    expander = np.ones((1, number_of_patches)) #1 X number_of_patches, adds constant to each training example
    
    #Forward Pass
    a0 = sigmoid(dot(data,W1) + b1) 
    a1 = sigmoid(dot(a0,W2) + b2)
    
    #L2-norm of error
    L2 = np.sum((a1 - data) ** 2) / (2. * number_of_patches)#not checked
    
    #Back-Propagation calculation of gradients
    delta3 = (a1 - data)* a1* (1-a1)
    W2grad = dot(a0.T,delta3) / number_of_patches# average gradient
    b2grad = dot(expander, delta3) / number_of_patches
    
    #Sparsity
    average_activations = dot(expander, a0) / number_of_patches
    sparsity_error = (-sparsityParam/ average_activations) +  ((1 - sparsityParam)/ (1 - average_activations)) 
    KL_divergence = np.sum( sparsityParam * np.log( (sparsityParam/average_activations)) + (1 - sparsityParam) * np.log( (1-sparsityParam)/(1-average_activations) ))
    
    delta2 =  (dot(delta3, W2.T) + dot(expander.T, sparsity_error) * beta) * a0 * (1-a0)  
    W1grad = dot(data.T, delta2) / number_of_patches #average gradient
    b1grad = dot(expander, delta2) / number_of_patches#average b1
    
    #Update gradient
    cost = L2 + (lambd / 2.) * ( np.sum(W1**2) + np.sum(W2**2)  ) + beta * KL_divergence#change norm
    W1grad = W1grad + lambd * W1
    
    W2grad = W2grad + lambd * W2
    """
    %-------------------------------------------------------------------
    % After computing the cost and gradient, we will convert the gradients back
    % to a vector format (suitable for minFunc).  Specifically, we will unroll
    % your gradient matrices into a vector.
    """
    grad = np.concatenate((W1grad.ravel(), W2grad.ravel(),b1grad.ravel(),b2grad.ravel()))
    
    return (cost, grad)
    
    

def sigmoid(x):
    
    return 1 / (1 + np.exp(-x))
    
